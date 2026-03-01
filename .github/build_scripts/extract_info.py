import os
import re
import shutil

mainDir = os.getcwd()
workDir = os.path.join(mainDir, "app", "build", "outputs", "apk")

extension = ".apk"


def setEnvValue(key, value):
    print(f"Setting env variable: {key}={value}")
    os.system(f"echo \"{key}={value}\" >> $GITHUB_ENV ")


def getAPKs():
    apk_list = []
    for root, dirs, files in os.walk(workDir):
        for file in files:
            if file.endswith(extension):
                apk_list.append([root, file])
    return apk_list


def processAPK(path, fileName):
    fileNamePath = os.path.join(path, fileName)
    # Match: WebnovelReader_v2.3.9-release.apk or similar patterns
    match = re.match(r"^(.+)_v(\d+\.\d+\.\d+)(?:-(.+))?\.apk$", fileName)
    if not match:
        print(f"Skipping unmatched file: {fileName}")
        return
    
    name = match.group(1)
    version = match.group(2)
    build_type = match.group(3) if match.group(3) else "release"

    newFileName = f"NovelDokusha_v{version}.apk"
    newFileNamePath = os.path.join(path, newFileName)

    shutil.move(fileNamePath, newFileNamePath)

    print(f"{name=} {version=} {newFileName=}")

    setEnvValue("APP_VERSION", version)
    setEnvValue("APK_FILE_PATH_foss", newFileNamePath)


for [path, fileName] in getAPKs():
    processAPK(path, fileName)
