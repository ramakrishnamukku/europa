export function modifyProperty(state, data) {
  return {
    ...state,
    ...data
  };
}