/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export function modifyProperty(state, data) {
  return {
    ...state,
    ...data
  };
}