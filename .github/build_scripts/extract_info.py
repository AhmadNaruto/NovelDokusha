import os
import re
import shutil

mainDir = os.getcwd()
workDir = os.path.join(mainDir, "app", "build", "outputs", "apk")
extension = ".apk"

def setEnvValue(key, value):
    print(f"Setting env varaible: {key}={value}")
    os.system(f"echo \"{key}={value}\" >> $GITHUB_ENV ")

def getAPKs():
    list = []
    for root, dirs, files in os.walk(workDir):
        for file in files:
            if file.endswith(extension):
                print("File ditemukan : " + file)
                list.append([root, file])
    return list

# Keterangan Pola Baru:
# ^(.+)       -> Grup 1: Nama dasar aplikasi (misalnya WebnovelReader)
# [._]v        -> Pemisah (titik/garis bawah) diikuti 'v'
# (\d+\.\d+\.\d+) -> Grup 2: Versi (misalnya 2.3.9)
# [.-_]        -> Pemisah (strip/titik/garis bawah)
# (.+)         -> Grup 3: Flavor/Build Type (misalnya release)
# \.apk$       -> Diakhiri dengan .apk

# pattern = r"^(.+)[._]v(\d+\.\d+\.\d+)[.-_](.+)\.apk$"

# Terapkan di dalam fungsi processAPK:
def processAPK(path, fileName):
    match = re.match(r"^(.+)_v(\d+\.\d+\.\d+)-(.+)\.apk$", fileName)
    
    if match is None:
        print(f"⚠️ Melewati file: {fileName} (Tidak cocok dengan pola RegEx)")
        return
        
    name, version, flavour = match.groups()
    newFileName = f"NovelDokusha_v{version}_{flavour}.apk" 

    shutil.move(
      os.path.join(path, fileName), os.path.join(path, newFileName)
      )
    print(f"file {newFileName} sudah diproses")
    print(f"{name=} {version=} {newFileName=}")

    setEnvValue("APP_VERSION", version)
    setEnvValue(f"APK_FILE_PATH_{flavour}", os.path.join(path, newFileName))


for [path, fileName] in getAPKs():
    processAPK(path, fileName)
