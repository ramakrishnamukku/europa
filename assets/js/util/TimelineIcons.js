/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function TimelineIcons(eventType){

  let icons = {
    'PUSH': '/assets/images/timeline-icons/event-push.svg',
    'DELETE': '/assets/images/timeline-icons/event-delete.svg'
  };

  let icon = icons[eventType];

  if(!icon) {
  	console.error(`No Icon for event type ${evenType}`);
  }

  return icon;
};