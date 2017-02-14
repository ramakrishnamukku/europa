export default function repoPullCommand(repoObj, dnsName, ctx = null){
	if (!repoObj) return;

	let provider = repoObj.provider;

	switch(provider) {

		case 'GCR':
			return `glcoud docker -- pull ${repoObj.region}/${repoObj.name}`;
		break;

		case 'ECR':
			if(repoObj.registryId == null) {
				    return null;
			} else {
				return `docker pull ${repoObj.registryId}.dkr.ecr.${repoObj.region}.amazonaws.com/${repoObj.name}`;	
			}
		break;

		case 'DOCKERHUB':
			return `docker pull ${repoObj.name}`;
		break;

		case 'PRIVATE':
			return `docker pull ${repoObj.endpoint}/${repoObj.name}`;

		case 'EUROPA':
			let endpoint = (repoObj.local) ? dnsName : repoObj.endpoint;
			let username = (ctx && ctx.username) ? (ctx.username + '/') : '';

			return `docker pull ${endpoint}/${username}${repoObj.name}`
		break;

		default:
			return null
	}
}