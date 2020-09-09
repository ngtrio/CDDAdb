pushd view/server
json-server --watch db.json --route  routes.json --port 4396
popd