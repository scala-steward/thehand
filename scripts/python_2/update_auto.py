import requests

from configuration import ConfServer
from configuration import ConfRepos

url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/updateall"

headers = {
    'X-API-Key': ConfServer.api,
    'cache-control': "no-cache",
    }

response = requests.request("POST", url, headers=headers)

print(response.text)


