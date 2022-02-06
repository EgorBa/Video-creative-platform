from bs4 import BeautifulSoup
import requests
import re
import io
from PIL import Image


def get_best_picture(all_links):
    best_url = ""
    best_url_size = 0
    for im_url in all_links:
        inp = io.BytesIO(requests.get(im_url).content)
        imageFile = Image.open(inp)
        (w, h) = imageFile.size
        if w * h > best_url_size:
            best_url = im_url
            best_url_size = w * h
    return best_url


def get_icon(url):
    result = requests.get(url)
    content = result.text
    soup = BeautifulSoup(content, 'lxml')
    quotes = soup.find_all('link', attrs={'rel': re.compile("^(shortcut icon|icon)$", re.I)})
    all_links = []
    for r in quotes:
        im_url = r["href"]
        if "https://" in im_url:
            all_links.append(im_url)
        else:
            all_links.append(url + im_url)
    return get_best_picture(all_links)


urls = ['https://pythonist.ru',
        'https://habr.com',
        'https://e.mail.ru',
        'https://stackoverflow.com',
        'https://tproger.ru',
        'https://www.endclothing.com',
        'https://kmu.itmo.ru',
        'https://www.sber.ru']
counter = 0
for url in urls:
    im_url = get_icon(url)
    print(im_url)
    if im_url != "":
        p = requests.get(im_url)
        inp = io.BytesIO(p.content)
        imageFile = Image.open(inp)
        imageFile.save("logos/" + str(counter) + ".png")
    counter += 1
