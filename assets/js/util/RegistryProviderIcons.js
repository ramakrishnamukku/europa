/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function RegistryProviderIcons(provider, useWhiteIcons=false) {
  let icon;

  let icons = {
    'GCR': '/public/images/registry-icons/gcr.svg',
    'ECR': '/public/images/registry-icons/ecr.svg',
    'DOCKERHUB': '/public/images/registry-icons/dockerhub.svg',
    'PRIVATE': '/public/images/registry-icons/private.svg',
    'EUROPA': '/public/images/registry-icons/europa.svg',
    "DELETED": '/public/images/registry-icons/europa.svg'
  };

  let whiteIcons = {
    'GCR': '/public/images/registry-icons/gcr-white.svg',
    'ECR': '/public/images/registry-icons/ecr-white.svg',
    'DOCKERHUB': '/public/images/registry-icons/dockerhub-white.svg',
    'PRIVATE': '/public/images/registry-icons/private-white.svg',
    'EUROPA': '/public/images/registry-icons/europa.svg',
    "DELETED": '/public/images/registry-icons/europa.svg'
  };

  if (useWhiteIcons) {
    icon = whiteIcons[provider];
  } else {
    icon = icons[provider];
  }

  if (!icon) {
  	// Error
  }

  return icon;
};