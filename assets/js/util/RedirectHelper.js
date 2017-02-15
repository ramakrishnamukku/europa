export function getRepoRedirect(repo) {
	if(repo.local) {
		 return repo.name;
	} else {
		return repo.id
	}
}