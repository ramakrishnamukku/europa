/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function RegistryNames(includeEuropa = false){

	let RegistryNames = {
		'GCR' : 'Google Container Registry',
		'ECR' : 'EC2 Container Registry',
		'DOCKERHUB' : 'DockerHub',
		// 'PRIVATE': 'Private Registry',
	};

	if(includeEuropa) {
		RegistryNames['EUROPA'] = 'Europa Container Registry';
	}

	return RegistryNames;
}