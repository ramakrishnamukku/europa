/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import RepoEventItem from './../components/RepoEventItem'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'

export default class RepoEventTimeline extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderLegend(){

		let navItems = [
			{
				text: 'Overview',
				key: 'OVERVIEW',
			},
			{
				text: 'Tags',
				key: 'TAGS',
			},
			{
				text: 'Events',
				key: 'EVENTS',
			}
		];

		return (
			<div className="TimelineLegend">
				<div className="TimelineNavigation">
					{navItems.map((item) => {
						let activeSection = NPECheck(this.props, 'repoDetails/timelineSection', '');
						return (
							<div key={item.key} 
								 className={(activeSection == item.key) ? 'Active' : ''}
								 onClick={() => this.context.actions.setTimelineSection(item.key)}>
								{item.text}
							</div>
						);
					})}
				</div>
				{this.renderLoadingIcon()}
			</div>
		);
	}
	renderLoadingIcon(){
		if(!NPECheck(this.props, 'events/length', true)) {
			return (
				<i className="icon icon-dis-waiting rotating"/>
			);
		}
	}
	renderRepoContent(){
		let activeSection = NPECheck(this.props, 'repoDetails/timelineSection', '');
		let noEvents = (!NPECheck(this.props, 'events/length', true));

		switch(activeSection) {

			case 'OVERVIEW':
				return (
					<div>
						This is the overview
					</div>
				);
			break;

			case 'EVENTS':
				if(noEvents) return this.renderNoEvents();
				return (
					<div className="TimelineContainer">
						{this.renderEventTimeline()}
					</div>	
				);
			break;

			case 'TAGS':
				if(noEvents) return this.renderNoEvents();
				return (
					<div className="TagsContainer">
						{this.renderRepoEventTags()}
					</div>
				);
			break;
		}
	}
	renderEventTimeline(){
		return this.props.events.sort((firstEvent, secondEvent) => (firstEvent.eventTime >= secondEvent.eventTime) ? -1 : 1 )
								.map((event, index) => this.renderRepoEventItem(event, index))
	}
	renderRepoEventItem(event, index){
		return (
			<RepoEventItem {...this.props}
						   key={index}
						   event={event} />
		);
	}
	renderRepoEventTags(){
		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});

		return this.props.events.sort((firstEvent, secondEvent) => (firstEvent.eventTime >= secondEvent.eventTime) ? -1 : 1 )
								.map((event, index) => this.renderRepoEventTagItem(event, index, activeRepo))
	}
	renderRepoEventTagItem(event, index, activeRepo){
		let time = event.eventTime;
		let friendlyTime = ConvertTimeFriendly(time);

		return (
			<div key={index} className="RepoTagItem">
				<img className="ProviderIcon" src={RegistryProviderIcons(activeRepo.provider)}/>
				<span className="ImageSha">
					{event.imageSha}
				</span>
				<span className="Tags">
					{event.imageTags.map((tag, index) => {
						return (
							<span className="Tag" key={index}>{tag}</span>
						);
					})}
				</span>
				<span className="Pushed">
					<span className="Label">Pushed:&nbsp;</span>
					<span className="Value">{friendlyTime}</span>
				</span>
			</div>
		);
	}
	renderNoEvents(){
		return (
			<div className="TimelineContainer">
				<div className="Timeline">
					<div className="NoContent">
						<h3>
							No Events Found
						</h3>
						<p> If you just added this repository, it may take a second to populate historical events.</p>
					</div>
				</div>
			</div>
		);
	}
	render() {
		return (
			<div className="RepoEventTimeline">
				<div className="Timeline">
					{this.renderLegend()}
					{this.renderRepoContent()}
				</div>
			</div>
		);
	}	
}

RepoEventTimeline.propTypes = {
	events: PropTypes.array.isRequired
};

RepoEventTimeline.childContextTypes = {
    actions: PropTypes.object
};

RepoEventTimeline.contextTypes = {
    actions: PropTypes.object
};

