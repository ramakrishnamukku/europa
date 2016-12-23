/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function RegistryProviderIcons(provider){

  let icons = {
    'GCR': '/assets/images/registry-icons/gcr.svg',
    'ECR': '/assets/images/registry-icons/ecr.svg',
    'DOCKERHUB': '/assets/images/registry-icons/dockerhub.svg',
    'PRIVATE': '/assets/images/registry-icons/private.svg',
  };

  let icon = icons[provider];

  if(!icon) {
  	console.error(`No Icon for Provider ${provider}`);
  }

  return icon;
};