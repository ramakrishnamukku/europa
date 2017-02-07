/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import ContentRow from './../components/ContentRow'
import RepoNotifications from './../components/RepoNotifications'
import CenteredConfirm from './../components/CenteredConfirm'
import RadioButton from './../components/RadioButton'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'
import isEmpty from './../util/IsEmpty'


export default class RepoSettings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		let repoId = this.props.repoDetails.activeRepo.id;
		this.context.actions.listRegistries();
		this.context.actions.listRepoNotifications(repoId);
	}
	componentWillUnmount() {
		this.context.actions.resetNotifState();
	}
	setRepoPublic(isPublic){
		this.context.actions.setRepoPublic(isPublic)
		.then(() => this.context.actions.listRepos(this.props.activeRepo.id))
		.then(() => this.context.actions.setActiveRepoDetails(this.props.activeRepo.id));
	}
	renderCredentials() {
		let creds = this.props.registriesMap[this.props.activeRepo.credId]
		if(!creds) return;
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
				{this.renderRepoDetails(creds)}
			</div>
		);
	}
	renderRepoDetails(creds){
		if(creds.provider == 'ECR') {
			return (
				<div className="FlexRow">
					<div className="Flex1">
						<label className="small">Public Key</label>
						<div className="Value">{creds.key}</div>
					</div>
					<div className="Flex1">
						<label className="small">Private Key</label>
						<div className="Value">******************</div>
					</div>
				</div>
			);
		}
	}
	renderRepoNotifications(){
		let notifs = this.props.notif.notifs;
		return (
			<RepoNotifications {...this.props} notifs={notifs}/>
		);
	}
	renderPublicSettings(){
		let repo = this.props.activeRepo;

		let error = NPECheck(this.props, 'repoDetails/publicError', false);

		if(error) {
			return (
				<Msg text={error} 
				 	 close={() => this.context.actions.clearRepoDetailsErrors()}
				 	 style={{padding: '1rem 0'}}/>
			);
		}

		let publicAction = repo.publicRepo ? () => {} : () => this.context.actions.confirmPublicStatusChange(true)
		let privateAction = !repo.publicRepo ? () => {} : () => this.context.actions.confirmPublicStatusChange(false)

		return (
			<div className="FlexColumn">
				<div className="FlexRow SpaceBetween">
					<div className="FlexRow">
						<label>Repository</label>
					</div>
				</div>
				<div className="FlexColumn">
					<div className="FlexRow">
						<div className="Column">
							<RadioButton onClick={publicAction} 
										 isChecked={repo.publicRepo}
										 label="Public" />
						</div>
						<div className="Column">
							<RadioButton onClick={privateAction} 
								  		 isChecked={!repo.publicRepo}
										 label="Private" />
						</div>
					</div>
					{this.renderPublicStatusChange()}
				</div>
			</div>
		);
	}
	renderPublicStatusChange(){
		if(NPECheck(this.props, 'repoDetails/publicXHR', false)) {
			return (
				<Loader />
			);
		}


		if(NPECheck(this.props, 'repoDetails/publicConfirm', false)) {

			let publicStatusChange = NPECheck(this.props, 'repoDetails/publicStatusChange', false);

			return (
				<CenteredConfirm message={`Are you sure you want make this repo ${(publicStatusChange) ? 'public' : 'private'}?`}
							     confirmButtonText="Yes"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.setRepoPublic(publicStatusChange)}
							     onCancel={() => this.context.actions.confirmPublicStatusChange() }/>
			);
		}
	}
	renderSettings(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-repo',
                renderBody: this.renderPublicSettings.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-credential',
                renderBody: this.renderCredentials.bind(this),
                condition: !!this.props.registriesMap[this.props.activeRepo.credId]
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

		let content = this.renderSettings();

		if(NPECheck(this.props, 'notif/notifsXHR', false) || NPECheck(this.props, 'registriesXHR', false)) {
			content = (
				<div className="PageLoader" style={{height: '300px'}}>
					<div style={{marginTop: '150px'}}>
						<Loader />
					</div>
				</div>
			);
		}

		return (
			<div className="RepoSettingsContainer">
				{content}
			</div>
		);
	}
}

RepoSettings.propTypes =  {
	activeRepo: PropTypes.object.isRequired
};

RepoSettings.childContextTypes = {
	actions: PropTypes.object
};

RepoSettings.contextTypes = {
	actions: PropTypes.object
};
