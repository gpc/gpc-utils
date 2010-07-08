#!/usr/bin/env groovy 

evaluate(new File("lib/lib.groovy"))

println "GPC Members (from members file):"
members.each {
	println "	- $it"
}
println ""


def repos = []
println "Getting repository listing…"
github.get(path: "repos/show/$username").with {
	data.repositories.each {
		def repo = it.name
		repos << repo
	}
}
println "	- done (${repos.size()} repos).\n"

repos.each { repo ->
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