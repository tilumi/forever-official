# -*- coding:gb2312 -*-
# -*- coding:UTF-8 -*-
__author__ = 'jimmy'

import urllib
import urllib2
import cookielib
import re
import json
import BeautifulSoup
from pattern import web


def createRequest(url,body=None):
    # print body
    if body !=None:
        body=urllib.urlencode(body)
    req=(urllib2.Request(url,body))
    req.add_header('User-agent','Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36')
    return req

def get_tables(html):
    """Parse html and return html tables of wikipedia population data."""

    dom = web.Element(html)

    ### 0. step: look at html source!
    #### 1. step: get all tables
    #tbls = dom('table')
    #### 2. step: get all tables we care about
    tbls = dom.by_class('collapse1')
#     href = tbls.by_class('thumbnail')
    return tbls

def getinfo(x):
    fishProfileRequest=createRequest('https://ms.tcgm.tw/FishBox/manage_display/show_fishStudyInfo/',{'fish_id':x})
    fishProfileResponse=httpClient.open(fishProfileRequest)
    fishProfileResponseString=fishProfileResponse.read()
    profilesoup = BeautifulSoup.BeautifulSoup(fishProfileResponseString)

    # div = profilesoup.findAll("div" , {'class' : 'span3 lightblue'})
    div = profilesoup.findAll('label')
    # print div
    str = div[1].text.encode('unicode-escape')
    list = str.split(":")
    if list[1].decode('unicode-escape') == u'\u7537':
        gender = True
    else :
        gender = False
    # gender = "u'%s'"%list[1]
    # print type(gender)
    # print gender


    str = div[3].text.encode('unicode-escape')
    list = str.split(":")
    depart = list[1].decode('unicode-escape')
    # print depart

    tag = profilesoup.findAll('h3')
    name = tag[0].text
    # print name
    if depart == u'\u570b\u9ad8\u4e2d\u90e8' and gender == True:
        order = 1
    elif depart == u'\u570b\u9ad8\u4e2d\u90e8' and gender == False:
        order = 2
    elif depart == u'\u5927\u5b78\u90e8' and gender == True:
        order = 3
    elif depart == u'\u5927\u5b78\u90e8' and gender == False:
        order = 4
    elif depart == u'\u9752\u5e74\u90e8' and gender == True:
        order = 5
    elif depart == u'\u9752\u5e74\u90e8' and gender == False:
        order = 6
    elif depart == u' \u9577\u5e74\u90e8' and gender == True:
        order = 7
    elif depart == u' \u9577\u5e74\u90e8' and gender == False:
        order = 8



    array = []
    for td_tag in profilesoup.findAll('td'):
        # str = td_tag.text.encode('big5')
        # print str
        # print td_tag
        array.append(td_tag.text)

    lecture = []
    date = []
    lecturer = []
    condition = []
    leclist = []
    i = 0
    j = 0
    while i <len(array):
        if j == 0:
            if array[i] == u'\u8056\u7d93\u6642\u89c0' :
                j = 1
            else :
                i = i+1

        if j == 1:
            if array[i] == u'\u5176\u4ed6' :
                i = len(array)
                break
            if array[i+1] == u'':
                lecture.append(array[i])
                date.append(u'0')
                lecturer.append(u'0')
                condition.append(u'0')
                i = i+2
            else :
                lecture.append(array[i])
                a = array[i+1].decode('unicode-escape').replace("-0","/").replace("-", "/")
                date.append(a)
                lecturer.append(array[i+2])
                condition.append(array[i+3])
                leclist.append([array[i],a,array[i+2]])
                i=i+5

    # reg = re.compile()
    # print repr(lecture).decode("unicode-escape")
    # print repr(date).decode("unicode-escape")
    # print repr(lecturer).decode("unicode-escape")
    # print repr(condition).decode("unicode-escape")
    # return [name,gender,depart,leclist]
    return {"name": name, "gender": gender, "depart": depart, "order": order, "lessons": leclist}

from flask import Flask
app = Flask(__name__)
cookieStore=cookielib.CookieJar()
httpClient=urllib2.build_opener(urllib2.HTTPCookieProcessor(cookieStore))

@app.route("/get_eva_status")
def get_eva_status():
    urlMap = {'loginPage' : 'https://ms.tcgm.tw/',
              'loginPost' : 'https://ms.tcgm.tw/welcome/login',
              'loginPostReferer' : ''}


    loginPageRequest=createRequest(urlMap.get('loginPage'))
    loginPageResponse=httpClient.open(loginPageRequest)
    loginPageResponseString=loginPageResponse.read()
    dom=BeautifulSoup.BeautifulSoup(loginPageResponseString)
    # print dom

    loginParameterMap = {'account':'jimmytshs42@gmail.com',
                         'pwd' : 'zxc5168A'}
    loginPostRequest = createRequest(urlMap.get('loginPost'),loginParameterMap)
    loginPostResponse=httpClient.open(loginPostRequest)
    loginPostResponseString=loginPostResponse.read()


    fishListPageRequest=createRequest('http://ms.tcgm.tw/FishBox/manage_display/show_fishList')
    fishListPageResponse=httpClient.open(fishListPageRequest)
    fishPageResponseString=fishListPageResponse.read()

    soup = BeautifulSoup.BeautifulSoup(fishPageResponseString)
    learninglist = soup.find(id = 'collapse1')
    href = learninglist.findAll('a', attrs={'href': re.compile("^javascript:formSubmit")})

    regex = re.compile("formSubmit\(([0-9]+)\)")

    fishMap = []


    for line in href:
        matches = regex.search(line.__repr__())
        if matches is not None:
            fishMap.append(matches.group(1))

    getfishinfo = []
    for i in fishMap:
        a = getinfo(i)
        getfishinfo.append(a)

    jsonlist = json.dumps(getfishinfo)
    return jsonlist

if __name__ == "__main__":
    app.run()
# print get_eva_status()