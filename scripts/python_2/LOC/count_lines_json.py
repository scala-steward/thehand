import os
import requests

from configuration import ConfServer
from configuration import ConfRepos

def file_len(fname):
    i = 0
    with open(fname) as f:
        for i, _ in enumerate(f):
            pass
    return i + 1

def generate_file_lines(top_dir):
    if os.path.exists('lines.txt'):
        os.remove('lines.txt')
    f = open('lines.txt', 'w')
    top_dir_remove = top_dir+"/"
    f.write('[\n')
    for dirpath, _, files in os.walk(top_dir):
        for file in files:
            if file.endswith(".cpp") or \
            file.endswith(".CPP") or \
            file.endswith(".h") or  \
            file.endswith(".H") or \
            file.endswith(".c") or \
            file.endswith(".C") or \
            file.endswith(".cmake") or \
            file.endswith(".ui") or \
            file.endswith(".UI") or \
            file.endswith(".qrc") or \
            file.endswith(".QRC") or \
            file.endswith(".rc") or \
            file.endswith(".RC") or \
            file.endswith(".rh") or \
            file.endswith(".RH") or \
            file.endswith(".ts") or \
            file.endswith(".TC") or \
            file.endswith("CMakeLists.txt"):
                complete_path = os.path.join(dirpath, file)
                f.write('{\n"path":"' + complete_path.replace("\\", "/").replace(top_dir_remove, "/trunk/") + '",\n"counter": ' + str(file_len(complete_path)) + '\n},\n')

    f.write('{"path": "", "counter": 0}]')
    f.close()

def upload_data():
    file = open('lines.txt')
    payload = file.read()
    headers = {
        'X-API-Key': ConfServer.api,
        'Content-Type': "application/json",
        'cache-control': "no-cache"
        }
    response = requests.request("POST", url, data=payload, headers=headers)
    print(response.text)

topdir = "."
for i in ConfRepos.res:
    print("Begin scan in " + i[1])
    url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/"+i[0]+"/loc"
    generate_file_lines(i[1])
    print("Scan ended")
    print("Begin upload to " + ConfServer.ip)
    upload_data()
    print("Upload ended")
