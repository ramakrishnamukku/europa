export default function AddRegistryReducers(state, action) {
    switch (action.type) {
        case 'UPDATE_NEW_REGISTRY':
        	return updateNewRegistry(state, action.data);
        
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