#!/usr/bin/env groovy 

evaluate(new File("lib/lib.groovy"))

def repo = argOrRead(0, "Enter the name of the repo to fork (without the username):")
if (repoExists(repo)) {
	die("GPC already has a repo with that name")
} 

def user = argOrRead(1, "Enter the name of the user who owns the repo:")

println "Forking repo '$user/$repo' …"
github.post(path: "repos/fork/$user/$repo")
println "	- done."