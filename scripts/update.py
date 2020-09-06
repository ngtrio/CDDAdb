import requests
from bs4 import BeautifulSoup
import zipfile
import os
import polib

# experiment builds page
exp_url = 'https://cataclysmdda.org/experimental/'

# path extract to 
ext_path = 'data/cdda'

# po file path
po_path = 'data/lang.po'

def _gen_i18n_file(local: str):
    """

    Args:
        local (str): e.g. zh_CN, ja, fr, ru, zh_TW... see: data/cdda/lang
    """
    path = f'data/cdda/lang/mo/{local}/LC_MESSAGES/cataclysm-dda.mo'
    if os.path.exists(path):
        mo = polib.mofile(path)
        mo.save_as_pofile(po_path)
        print(f'Mo file of {local} is decompiled to {po_path}!')
    else:
        print(f'Mo file of {local} is not found!')

def _get_latest_url() -> str:
    body = requests.get(exp_url).text
    bs = BeautifulSoup(body, 'html.parser')

    title = bs.h2.text
    print(f'Latest experimental build: {title}')

    build_list = bs.ul.find_all('a')
    for build in build_list:
        name: str = build.text
        if name.find("Windows_x64") != -1:
            return build['href']
    return ''

def _download() -> str:
    dl_url = _get_latest_url()
    if dl_url == '':
        print('Getting the latest build failed, exit...')
        return ''
    res = requests.get(dl_url, stream=True)
    cd = res.headers['content-disposition']
    filename = cd[cd.find('filename=')+9:]
    filepath = f'data/{filename}'
    MB = 1024 * 1024
    size = int(res.headers['content-length']) / MB

    print(f'Downloading to data/{filename}, size: {size}MB')

    with open(filepath, 'wb') as file:
        for data in res.iter_content(MB):
            file.write(data)
    print('Done!!')
    return filepath

def _extract(filepath):
    print(f'Extracting {filepath} to {ext_path} ...')
    with zipfile.ZipFile(filepath) as file:
        file.extractall(ext_path)
    print('Done!！')

if __name__ == "__main__":
    filepath = _download()
    if filepath != '':
        _extract(filepath)
        _gen_i18n_file('zh_CN')
    else:
        print('Update failed, exit...')