/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function AddRegistryReducers(state, action) {
    switch (action.type) {
        case 'UPDATE_NEW_REGISTRY':
        	return updateNewRegistry(state, action.data);

        case 'UPDATE_TOKENS_STATE':
        	return updateTokensState(state, action.data);

        case 'UPDATE_STORAGE_CREDS':
        	return updateStorageCreds(state, action.data);

        default:
            return state;
    }
}

function updateNewRegistry(state, data){
	return {
		...state,
		newRegistry: {
			...state.newRegistry,
			[data.prop]: data.value
		}
	};
}


function updateTokensState(state, data) {
	return {
		...state,
		tokens: {
			...state.tokens,
			...data
		}
	};
}


function updateStorageCreds(state, data){
	return {
		...state,
		storageCreds: {
			...state.storageCreds,
			...data
		}
	};
}
