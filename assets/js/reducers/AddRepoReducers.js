export default function AddRepoReducers(state, action) {
    switch (action.type) {
        case 'UPDATE_NEW_REPO':
        	return updateNewRepo(state, action.data);
        
        default:
            return state;
    }
}

function updateNewRepo(state, data){
	let newRepo = {
		...state.newRepo
	};

	let keys = data.value.split('/');
	if(keys.length > 1) {
		newRepo = keys.reduce((cur, key) => {
			return cur[key]
		}, newRepo) 
	}

	console.log(newRepo)

	return {
		...state,
		newRepo: {
			...state.newRepo,
		}
	};
}