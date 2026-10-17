import UIKit
import NodysseyShell

/// The whole of the Swift in this app.
///
/// `MainActivity` is the Android counterpart and it is 76 lines; this is short, and the difference is
/// not that iOS does less — it is that everything `MainActivity` does beyond installing a composition
/// is about the platform's own hooks, and this app has one so far: the offline downloader's
/// background tasks. `registerBackgroundTasks` installs their `BGTaskScheduler` handlers, which
/// **must** happen before launch finishes, so it goes here rather than in the scene. When
/// notifications and Universal Links arrive, this is where they land too.
@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        NodysseyApp.shared.registerBackgroundTasks()
        DictationCrashWorkaround.install()
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting session: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        UISceneConfiguration(name: "Default", sessionRole: session.role)
    }
}

/// Where the window is made, which since iOS 26 is the only place it may be made.
///
/// The pre-scene shape — an app delegate that builds its own `UIWindow` in
/// `didFinishLaunchingWithOptions` — no longer merely warns: `UIKit` traps at launch in
/// `_UIApplicationEvaluateRuntimeIssueForNoSceneLifecycleAdoption` unless `Info.plist` declares a
/// `UIApplicationSceneManifest` and something adopts it. That trap is what the first run of this
/// target did, and it looks exactly like an app that will not start.
///
/// The two-step root is `NodysseyApp.start`'s doing: the app's `User-Agent` has to be the one WebKit
/// sends, WebKit will only say what that is asynchronously, and building the graph around a guess
/// would mean throwing away a `cf_clearance` the moment the real answer arrived. So the window comes
/// up empty for the few milliseconds that takes — which is what a launch screen is for.
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        self.window = window
        window.rootViewController = UIViewController()
        window.makeKeyAndVisible()

        NodysseyApp.shared.start { controller in
            window.rootViewController = controller
        }
    }
}

/// Keeps iOS dictation from crashing every Compose text field.
///
/// When dictation puts its first words in a field, UIKit calls `setMarkedText:selectedRange:` with
/// `selectedRange.location == NSNotFound` (Int.max). Compose Multiplatform 1.13's text input views
/// (`ComposeTextInputView` / `NativeTextInputView`) convert that with `location.toInt()`, which
/// truncates to -1, and then build `TextRange(-1, -1)` — whose constructor rejects negatives and
/// throws, taking the app down. The pinyin keyboard always passes a real range, which is why typing
/// was fine and only dictation crashed.
///
/// Compose does nothing with that range beyond building it, so clamping it into the marked text
/// before Compose sees it changes no editing behaviour; it only removes the illegal value. The views
/// are Kotlin classes, so they are found by name in the Objective-C runtime and patched in place.
/// Kotlin/Native may register them only once first used, so the scan runs again whenever a
/// keyboard is about to appear until the default view has been patched.
///
/// Remove this once Compose Multiplatform handles `NSNotFound` itself.
enum DictationCrashWorkaround {
    private static let selector = NSSelectorFromString("setMarkedText:selectedRange:")
    private static var patched = Set<String>()
    private static var observer: NSObjectProtocol?

    static func install() {
        patchLoadedClasses()
        guard !isDone else { return }
        observer = NotificationCenter.default.addObserver(
            forName: UIResponder.keyboardWillShowNotification,
            object: nil,
            queue: .main
        ) { _ in
            patchLoadedClasses()
            if isDone, let token = observer {
                NotificationCenter.default.removeObserver(token)
                observer = nil
            }
        }
    }

    private static var isDone: Bool {
        patched.contains { $0.hasSuffix("ComposeTextInputView") }
    }

    private static func patchLoadedClasses() {
        var count: UInt32 = 0
        guard let classes = objc_copyClassList(&count) else { return }
        defer { free(UnsafeMutableRawPointer(classes)) }
        for index in 0..<Int(count) {
            let cls: AnyClass = classes[index]
            let name = String(cString: class_getName(cls))
            guard name.hasSuffix("ComposeTextInputView") || name.hasSuffix("NativeTextInputView"),
                  !patched.contains(name),
                  let method = ownMethod(of: cls)
            else { continue }
            patch(method)
            patched.insert(name)
        }
    }

    /// The class's own implementation, so a subclass is never patched through its superclass twice.
    private static func ownMethod(of cls: AnyClass) -> Method? {
        var count: UInt32 = 0
        guard let methods = class_copyMethodList(cls, &count) else { return nil }
        defer { free(methods) }
        for index in 0..<Int(count) where method_getName(methods[index]) == selector {
            return methods[index]
        }
        return nil
    }

    private static func patch(_ method: Method) {
        typealias SetMarkedText = @convention(c) (AnyObject, Selector, NSString?, NSRange) -> Void
        let original = unsafeBitCast(method_getImplementation(method), to: SetMarkedText.self)
        let sel = selector
        let replacement: @convention(block) (AnyObject, NSString?, NSRange) -> Void = { view, text, range in
            let length = text?.length ?? 0
            var safe = range
            if safe.location == NSNotFound || safe.location > length {
                safe = NSRange(location: length, length: 0)
            } else if safe.length > length - safe.location {
                safe.length = length - safe.location
            }
            original(view, sel, text, safe)
        }
        method_setImplementation(method, imp_implementationWithBlock(replacement))
    }
}
