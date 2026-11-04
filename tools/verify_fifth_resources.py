"""Check the untouched original resources and root executable against the approved base."""
import subprocess

tree = subprocess.check_output(['git','ls-tree','-r','ca4b3ad','--','src/main/resources','TheLittleFish.jar'], text=True)
expected_blobs = []
for line in tree.splitlines():
    info, path = line.split('\t',1)
    if path == 'src/main/resources/maps/stage4.txt':
        continue  # The only original resource intentionally redesigned by this task.
    expected_blobs.append((path, info.split()[2]))
# Git applies the checkout's text normalization; PNG/WAV/JAR still compare exact bytes.
actual = subprocess.check_output(['git','hash-object','--stdin-paths'],
    input='\n'.join(path for path,_ in expected_blobs)+'\n', text=True).splitlines()
for (path, expected), observed in zip(expected_blobs, actual, strict=True):
    if expected != observed:
        raise SystemExit(f'Unexpected change to original resource: {path}')
print(f'PRESERVATION PASS: {len(actual)} original resource/JAR blobs unchanged vs ca4b3ad; only stage4.txt exempted')
