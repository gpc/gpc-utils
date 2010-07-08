#!/usr/bin/env groovy 

evaluate(new File("lib/lib.groovy"))

def repos = getRepos()
def reposToGrant = []
if (args) {
	args.each {
		def repo = it.trim()
		if (!repoExists(repo, repos)) {
			die("The repo '$repo' does not exist")
		}
		reposToGrant << repo
	}
} else {
	reposToGrant.addAll(repos)
}

println "GPC Members (from members file):"
members.each { println "	- $it" }
println ""

reposToGrant.each { repo ->
	println "Checking repo '$repo'…"
	def collaborators = []
	github.get(path: "repos/show/$username/$repo/collaborators").with {
		collaborators.addAll(data.collaborators)
	}
	collaborators.remove('gpc')
	
	def toAdd = members.findAll { !(it in collaborators) }
	toAdd.each { user ->
		println "	- adding '$user'"
		github.post(path: "repos/collaborators/$repo/add/$user")
	}
	
	def toRemove = collaborators.findAll { !(it in members) }
	toRemove.each { user ->
		println "	- removing '$user'"
		github.post(path: "repos/collaborators/$repo/remove/$user")
	}

	println "	- done.\n"
}

println "All done."