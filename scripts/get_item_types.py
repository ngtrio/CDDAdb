import glob
import json

types = set()
for filename in glob.iglob("../data/cdda/data/json/items/" + '**/*.json', recursive=True):
    with open(filename) as file:
        arr = json.load(file)
        for obj in arr:
            v = obj['type']
            if v:
                types.add(v)

print(types)
