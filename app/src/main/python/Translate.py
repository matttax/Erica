import time
import requests
from reverso_context_api import Client
from wiktionaryparser import WiktionaryParser
from translate import Translator


def translate(word, from_lang, to_lang):
    translator = Translator(to_lang=to_lang, from_lang=from_lang)
    return translator.translate(word)


def getDefinitions(word, lang):
    dictionary = {}
    parser = WiktionaryParser()
    parser.language = lang
    try:
        res = parser.fetch(word)
    except requests.exceptions.ConnectionError or requests.exceptions.ConnectTimeout:
        return dictionary
    definitions = []
    if len(res) > 0:
        for dats, i in zip(res, range(len(res))):
            if len(dats) > 0:
                definitions = dats['definitions']
            for defn in definitions:
                dictionary[str(i + 1) + ". " + defn['partOfSpeech']] = list(defn['text'])
    return dictionary


def getTranslations(word, from_lang, to_lang):
    client = Client(from_lang, to_lang)
    data = []
    timeout = time.time() + 2
    limit = 50
    try:
        for i, j in zip(client.get_translations(word), range(limit)):
            if j == limit or time.time() > timeout:
                break
            data.append(i)
    except requests.exceptions.ConnectionError or requests.exceptions.ConnectTimeout:
        return data
    except requests.exceptions.HTTPError:
        data.append('@HttpError@No examples are found')
    return data


def getExamples(word, from_lang, to_lang):
    client = Client(from_lang, to_lang)
    data = {}
    timeout = time.time() + 2
    limit = 100
    try:
        for i, j in zip(client.get_translation_samples(word), range(limit)):
            if j == limit or time.time() > timeout:
                break
            data[i[0]] = i[1]
    except requests.exceptions.HTTPError:
        data['@HttpError@'] = 'No examples are found'
    except requests.exceptions.ConnectionError or requests.exceptions.ConnectTimeout:
        data['@ConnectionError@'] = 'Problem with your connection'
    return data
