/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function RegistryProviderIcons(provider){

  let icons = {
    'GCR': '/public/images/registry-icons/gcr.svg',
    'ECR': '/public/images/registry-icons/ecr.svg',
    'DOCKERHUB': '/public/images/registry-icons/dockerhub.svg',
    'PRIVATE': '/public/images/registry-icons/private.svg',
    'EUROPA': '/public/images/registry-icons/europa.svg'
  };

  let icon = icons[provider];

  if(!icon) {
  	console.error(`No Icon for Provider ${provider}`);
  }

  return icon;
};