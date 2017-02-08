/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import RepoOverview from './RepoOverview'
import RepoEventTimeline from './RepoEventTimeline'
import RepoTags from './RepoTags'


export default class RepoDetailsContent extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderLegend(activeSection){
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
						return (
							<div key={item.key} 
								 className={(activeSection == item.key) ? 'Active' : ''}
								 onClick={() => this.context.actions.setTimelineSection(item.key)}>
								{item.text}
							</div>
						);
					})}
				</div>
				{this.renderHeaderAction(activeSection)}
			</div>
		);
	}
	renderHeaderAction(activeSection){
		switch(activeSection) {

			case 'OVERVIEW':
				if(NPECheck(this.props, 'repoDetails/timelineSection', '') == 'OVERVIEW') {
					let isEdit = NPECheck(this.props, 'repoDetails/editOverview', false);
					let actionText = (isEdit) ? (NPECheck(this.props, 'repoDetails/isOverviewModified')) ? 'Preview Changes' : 'Cancel' : 'Edit Read Me';

					if(!NPECheck(this.props, 'repoDetails/repoOverviewContent/length', true) && !NPECheck(this.props, 'repoDetails/isOverviewModified', true)) {
						actionText = 'Create Read Me'
					}

					return (
						<span className="ThickBlueText" onClick={() => this.context.actions.toggleRepoOverviewEdit()}>
							{actionText}
						</span>
					);
				}
			break;

			case 'EVENTS':
				if(!NPECheck(this.props, 'events/length', true)) {
					return (
						<i className="icon icon-dis-waiting rotating" data-tip="Polling for updates" data-for="ToolTipTop"/>
					);
				}
			break;

			case 'TAGS':
				if(!NPECheck(this.props, 'manifests/length', true)) {
					return (
						<i className="icon icon-dis-waiting rotating" data-tip="Polling for updates" data-for="ToolTipTop"/>
					);
				}
			break;
		}
	}
	renderRepoContent(activeSection){
		switch(activeSection) {

			case 'OVERVIEW':
				return (
					<RepoOverview {...this.props}/>
				);
			break;

			case 'EVENTS':
				return (
					<RepoEventTimeline {...this.props}
										events={this.props.events} />
				);
			break;

			case 'TAGS':
				return (
					<RepoTags {...this.props} 
							  manifest={this.props.manifests} />
				);
			break;
		}
	}
	render() {
		let activeSection = NPECheck(this.props, 'repoDetails/timelineSection', '');

		return (
			<div className="RepoEventTimeline">
				<div className="Timeline">
					{this.renderLegend(activeSection)}
					{this.renderRepoContent(activeSection)}
				</div>
			</div>
		);
	}	
}

RepoDetailsContent.propTypes = {
	events: PropTypes.array,
	manifests: PropTypes.array,
	
};

RepoDetailsContent.childContextTypes = {
    actions: PropTypes.object
};

RepoDetailsContent.contextTypes = {
    actions: PropTypes.object
};

