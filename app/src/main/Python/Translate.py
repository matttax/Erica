import itertools
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
        dats = res[0]
        if len(dats) > 0:
            definitions = dats['definitions']
    for defn in definitions:
        dictionary[defn['partOfSpeech']] = list(defn['text'])
    return dictionary


def getTranslations(word, from_lang, to_lang):
    client = Client(from_lang, to_lang)
    data = []
    try:
        data = list(itertools.islice(client.get_translations(word), 50))
    except requests.exceptions.ConnectionError or requests.exceptions.ConnectTimeout:
        return data
    except requests.exceptions.HTTPError:
        data.append('@HttpError@No examples are found')
    return data


def getExamples(word, from_lang, to_lang):
    client = Client(from_lang, to_lang)
    data = {}
    try:
        data = dict(list(itertools.islice(client.get_translation_samples(word), 100)))
    except requests.exceptions.HTTPError:
        data['@HttpError@'] = 'No examples are found'
    except requests.exceptions.ConnectionError or requests.exceptions.ConnectTimeout:
        data['@ConnectionError@'] = 'Problem with your connection'
    return data
