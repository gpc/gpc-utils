#!/usr/bin/env groovy 

evaluate(new File("lib/lib.groovy"))

def repo = argOrRead(0, "Enter the name of the repo to create:")
if (repoExists(repo)) {
	die("A repo already exists with that name")
} 

def description = argOrRead(1, "Enter the description:")

println "Creating repo with name '$repo' and description '$description' …"
github.post(
	path: 'repos/create',
	body: [
		name: repo,
		description: description,
		homepage: "http://gpc.github.com/$repo".toString(),
		'public': 1
	]
)
println "	- done."