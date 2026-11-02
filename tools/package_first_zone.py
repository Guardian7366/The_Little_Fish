"""Package this workspace release; never replaces the earlier stable distributions."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED, ZIP_STORED
import hashlib
import json
import shutil

project = Path(__file__).resolve().parents[1]
workspace = project.parents[1]
outputs = workspace / 'outputs'
game = outputs / 'TheLittleFish-PrimeraZona'
source = outputs / 'TheLittleFish-Proyecto-PrimeraZona'
jar = project / 'build/TheLittleFish.jar'
original = workspace / 'work/original/MyGame/MyGame.jar'

resources = sorted(p for p in (project / 'src/main/resources').rglob('*') if p.is_file())
changed = []
with ZipFile(original) as archive:
    for file in resources:
        name = file.relative_to(project / 'src/main/resources').as_posix()
        if file.read_bytes() != archive.read(name): changed.append(name)
assert changed == ['maps/stage1.txt'], changed

metadata = {
    'original_jar_sha256': hashlib.sha256(original.read_bytes()).hexdigest(),
    'restored_jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
    'original_resource_files': len(resources),
    'unchanged_resource_files': len(resources) - len(changed),
    'modified_resource_files': changed,
    'original_png_files_preserved': sum(p.suffix == '.png' for p in resources),
    'source_recovery': 'CFR 0.152; reconstructed from the supplied compiled JAR',
    'build': 'JDK 25, javac --release 17',
    'version': 'Primera zona renovada 2026-10-31',
    'audio_fix_included': True,
}
(project / 'ORIGEN.json').write_text(json.dumps(metadata, indent=2, ensure_ascii=False)+'\n', encoding='utf-8')

for folder in (game, source):
    folder.mkdir(parents=True, exist_ok=True)
    for file in project.glob('*.md'): shutil.copy2(file, folder / file.name)
    for name in ('Jugar.cmd', 'ORIGEN.json'): shutil.copy2(project / name, folder / name)
shutil.copy2(jar, game / jar.name)
for name in ('src', 'tools'): shutil.copytree(project / name, source / name, dirs_exist_ok=True)
for name in ('Compilar.cmd', 'build.ps1', '.gitignore'): shutil.copy2(project / name, source / name)

# A rendered game frame, not an edited asset. Useful as a release preview.
shutil.copy2(project / 'build/first-zone-preview/13-31.png', game / 'Vista-previa.png')

for folder in (game, source):
    destination = outputs / (folder.name + '.zip')
    with ZipFile(destination, 'w', compression=ZIP_DEFLATED, compresslevel=6) as archive:
        for file in sorted(p for p in folder.rglob('*') if p.is_file()):
            assert file.name not in ('save.dat', 'save-v1.dat', 'save-v1.dat.bak')
            archive.write(file, file.relative_to(outputs).as_posix(),
                          compress_type=ZIP_STORED if file.suffix == '.jar' else ZIP_DEFLATED)
    with ZipFile(destination) as archive:
        assert archive.testzip() is None, destination
    print(destination, destination.stat().st_size, 'bytes')
print('JAR SHA256', metadata['restored_jar_sha256'])
