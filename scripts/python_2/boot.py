import requests

from configuration import ConfServer
from configuration import ConfRepos

headers = {
    'X-API-Key': ConfServer.api,
    'cache-control': "no-cache",
    }

ip = ConfServer.ip
port = ConfServer.port

url = "http://"+ip+":"+port+"/boot/"+ConfServer.magic
response = requests.request("POST", url, headers=headers)
print(response.text)

for i in ConfRepos.res:
    url = "http://"+ip+":"+port+"/boot/"+i+"/"+ConfServer.magic
    response = requests.request("POST", url, headers=headers)
    print(response.text)
