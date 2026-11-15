# Contributing

Keep platform hooks independent and preserve native adaptive drawables. Resolve actual ROM/API differences at installation time and report unsupported features through the framework log. Avoid per-app package lists, destructive cache/data resets and replacement of unrelated system symbols.

Run the release build and Lint, then the inspection APK on the affected Android version. Changes to hook targets need a real framework/launcher APK inspection; changes to rendering need Android Canvas assertions or device screenshots. Record whether evidence is interface discovery, rendering, or live Xposed execution.

Do not commit keystores, device logs, private screenshots or local SDK paths. Reports should include only the sanitized CircleIcons log lines needed to reproduce the issue.
