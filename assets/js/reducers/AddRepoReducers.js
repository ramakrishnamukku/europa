export default function AddRepoReducers(state, action) {
    switch (action.type) {
        case 'UPDATE_NEW_REPO':
        	return updateNewRepo(state, action.data);
        
        default:
            return state;
    }
}

function updateNewRepo(state, data){
	return {
		...state,
		newRepo: {
			...state.newRepo,
			[data.prop]: data.value
		}
	};
}