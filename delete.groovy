#!/usr/bin/env groovy 

evaluate(new File("lib/lib.groovy"))

def repo = argOrRead(0, "Enter the name of the repo to delete:")

if (!repoExists(repo)) {
	die("There is no repo called '$repo'")
}

confirm "Are you sure you want to delete the '$repo' repository?"
confirm "Are you really sure?"

println "Deleting repo $repo …"
def path = "repos/delete/$repo"
github.post(path: path).with {
	github.post(path: path, body: [delete_token: data.delete_token])
}
println "	- done."