"""Run the real-Android rendering and interface checks on one explicitly selected device."""
import argparse
from pathlib import Path
import re
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--adb', default='adb')
parser.add_argument('--apk-dir', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
output = root / 'artifacts'
output.mkdir(exist_ok=True)

def adb(*command):
    return subprocess.check_output([args.adb, '-s', args.serial, *command], text=True, encoding='utf-8', errors='replace')

if args.apk_dir:
    app = args.apk_dir / 'app-inspection-debug.apk'
    tests = args.apk_dir / 'app-inspection-debug-androidTest.apk'
else:
    app = root / 'app/build/outputs/apk/inspection/debug/app-inspection-debug.apk'
    tests = root / 'app/build/outputs/apk/androidTest/inspection/debug/app-inspection-debug-androidTest.apk'
print(adb('install', '-r', str(app)), end='')
print(adb('install', '-r', str(tests)), end='')
api = int(adb('shell', 'getprop', 'ro.build.version.sdk').strip())
command = ['shell', 'am', 'instrument']
if api >= 28:
    command.append('--no-hidden-api-checks')
command.extend(['-w', 'com.yusd.circleicons.inspection.test/androidx.test.runner.AndroidJUnitRunner'])
result = adb(*command)
(output / f'inspection-api{api}.txt').write_text(result.strip() + '\n', encoding='utf-8')
print(result)
# `am instrument` may exit zero even when JUnit fails; only the test runner's success counts.
if not re.search(r'OK \(\d+ tests?\)', result) or 'FAILURES!!!' in result:
    raise SystemExit('Android inspection failed')
print(adb('logcat', '-d', '-s', 'CircleIconsInspection:I', '*:S'))
