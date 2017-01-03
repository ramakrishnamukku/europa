/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import ContentRow from './../components/ContentRow'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import RepoNotifications from './../components/RepoNotifications'
import isEmpty from './../util/IsEmpty'

export default class RepoSettings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillMount() {
		this.setState({
			activeRepoCreds: this.context.state.registriesMap[this.props.activeRepo.credId]
		});
	}
	componentDidMount() {
		let repoId = this.context.state.repoDetails.activeRepo.id;
		this.context.actions.listRepoNotifications(repoId);
	}
	renderCredentials() {
		let creds = this.state.activeRepoCreds;
		return (
			<div className="FlexColumn">
				<div className="FlexRow SpaceBetween">
					<div className="FlexRow">
						<label>Registry Credentials
							<span className="TealColor">&nbsp;-&nbsp;{creds.provider}</span>
						</label>
					</div>
					<div className="FlexRow">
						<i className="icon icon-dis-close" 
						   onClick={() => this.context.actions.toggleActiveRepoSettings()}/>
					</div>
				</div>
				<div className="FlexRow Row">
					<div className="Flex1">
						<label className="small">Key Name</label>
						<div className="Value">{creds.name}</div>
					</div>
					<div className="Flex1">
						<label className="small">Key Region</label>
						<div className="Value">{creds.region}</div>
					</div>
				</div>
				<div className="FlexRow">
					<div className="Flex1">
						<label className="small">Public Key</label>
						<div className="Value">{creds.key}</div>
					</div>
					<div className="Flex1">
						<label className="small">Private Key</label>
						<div className="Value">Hidden</div>
					</div>
				</div>
			</div>
		);
	}
	renderRepoNotifications(){
		let notifs = this.context.state.notif.notifs;
		return (
			<RepoNotifications notifs={notifs}/>
		);
	}
	renderSettings(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-credential',
                renderBody: this.renderCredentials.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-notification',
                renderBody: this.renderRepoNotifications.bind(this)
            }]
		}];

		return rows.map(this.renderContentRow);
	}
	renderContentRow(row, index){
		return (
			<ContentRow key={index}
						row={row} />
		);
	}
	render() {	
		return (
			<div className="RepoSettingsContainer">
				{this.renderSettings()}
			</div>
		);
	}
}

RepoSettings.propTypes =  {
	activeRepo: PropTypes.object.isRequired
};

RepoSettings.childContextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

RepoSettings.contextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
