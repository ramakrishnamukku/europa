/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import RepoOverview from './RepoOverview'
import RepoEventItem from './../components/RepoEventItem'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import Loader from './../components/Loader'


export default class RepoEventTimeline extends Component {
	constructor(props) {
		super(props);
		this.state = {
			pollEventsInterval: null
		};
	}
	componentDidMount() {
		let repoId = NPECheck(this.props, 'repoDetails/activeRepo/id', '');

		if(!NPECheck(this.props, 'repoDetails/hasRetrievedEvents', true)) {
			 this.context.actions.listRepoEvents(repoId);
		}

		this.setState({
			pollEventsInterval: setInterval(() => {
				let prevMarker = NPECheck(this.props, 'repoDetails/eventsPrevMarker', false);
				// Only poll if on first page of events
				if(!prevMarker) {
					this.context.actions.listRepoEvents(repoId, true);
				}
			}, 15000)
		});
	}
	componentWillUnmount() {
		clearInterval(this.state.pollEventsInterval);
	}
	renderRepoEventItem(event, index){
		return (
			<RepoEventItem {...this.props}
						   key={index}
						   event={event} />
		);
	}
	renderNoEvents(overrideContent){
		let content = [
			<h3 key={1}>
				No Events Found
			</h3>,
			<p key={2}> If you just added this repository, it may take a second to populate historical data for this repository.</p>
		];

		if(overrideContent) content = overrideContent;

		return (
			<div className="Timeline">
				<div className="NoContent">
					{content}
				</div>
			</div>
		);
	}
	render() {
		let content = NPECheck(this.props, 'events', []).sort((firstEvent, secondEvent) => (firstEvent.eventTime >= secondEvent.eventTime) ? -1 : 1 )
								 .map((event, index) => this.renderRepoEventItem(event, index));

		if(!this.props.events || !this.props.events.length) {
			content = this.renderNoEvents();
		}

		if(this.props.repoDetails.eventsXHR) {
			content =  this.renderNoEvents(<Loader />);
		}

		return (
			<div className="TimelineContainer">
				{content}
			</div>
		);
	}
}

RepoEventTimeline.propTypes = {
	events: PropTypes.array,
	manifests: PropTypes.array,

};

RepoEventTimeline.childContextTypes = {
    actions: PropTypes.object
};

RepoEventTimeline.contextTypes = {
    actions: PropTypes.object
};

