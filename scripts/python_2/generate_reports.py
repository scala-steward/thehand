import os
import requests
from datetime import datetime
from datetime import timedelta

from configuration import ConfServer
from configuration import ConfRepos

headers = {
    'X-API-Key': ConfServer.api,
    'Content-Type': "application/xml",
    'Accept': "*/*",
    'Cache-Control': "no-cache",
    'Accept-Encoding': "gzip, deflate",
    'Connection': "keep-alive",
    'cache-control': "no-cache"
    }

twoYearsAgo = datetime.now() - timedelta(days= 728)
today = datetime.now()

if not os.path.exists('reports'):
    os.makedirs('reports')

for i in ConfRepos.res:
    url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/"+i+"/commits/custom/Issue/"+twoYearsAgo.strftime("%Y-%m-%d")+"/to/"+today.strftime("%Y-%m-%d")+"/csv"
    #url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/"+i+"/commits/custom/Issue/2019-10-07/to/2019-10-07/csv"
    response = requests.request("GET", url, headers=headers)
    file = open('reports/'+ i+'issues.csv', 'w')
    file.write(response.text.replace('\r', '').encode('utf8'))
    file.close

for i in ConfRepos.res:
    url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/"+i+"/commits/custom/loc/Issue/"+twoYearsAgo.strftime("%Y-%m-%d")+"/to/"+today.strftime("%Y-%m-%d")+"/csv"
    #url = "http://"+ConfServer.ip+":"+ConfServer.port+"/api/v1/"+i+"/commits/custom/Issue/2019-10-07/to/2019-10-07/csv"
    response = requests.request("GET", url, headers=headers)
    file = open('reports/'+ i+'loc_issues.csv', 'w')
    file.write(response.text.replace('\r', '').encode('utf8'))
    file.close