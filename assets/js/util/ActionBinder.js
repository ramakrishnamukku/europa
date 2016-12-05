export default function actionBinder(actions, thisContext) {
  let combinedActions = Object.assign(...actions);
  let boundActions = {};
  for (let func in combinedActions) {
    boundActions[func] = combinedActions[func].bind(thisContext)
  }
  return boundActions;
}