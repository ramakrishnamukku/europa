export default function cleanSha(sha, length = 20){
	if(sha.substr(0,7) == 'sha256:') {
		return sha.substr(7, length + 7);
	}
	else {
		return sha.substr(0, length);
	}
}