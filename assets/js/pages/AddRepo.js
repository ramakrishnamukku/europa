/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'
import ContentRow from './../components/ContentRow'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import isEmpty from './../util/IsEmpty'
import NPECheck from './../util/NPECheck'

let dockerRepoNameKey = 'repo/name';
let targetKey = "notification/target";
let typeKey = "notification/type";
let secretKey = "notification/secret";

export default class AddRepository extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillUnmount() {
		this.context.actions.resetAddRepoState();
	}
	inputClassName(selector){
		let hasSelector = this.context.state.addRepo.errorFields.includes(selector)
		if(hasSelector) {
			return "BlueBorder FullWidth Error";
		} else {
		    return "BlueBorder FullWidth";
		}
	}		
	renderAddRegistry(){
		return (
			<AddRegistry 
				standaloneMode={false}
				isEdit={false}
			/>
		);
	}
	renderDockerRepository(){
		return (
			<div className="FlexColumn">
				<label>
					Docker Image Repository
				</label>
				<input className={this.inputClassName(dockerRepoNameKey)} 
				       placeholder="Enter Docker Repository.."
				       value={this.context.state.addRepo.newRepo[dockerRepoNameKey]}
					   onChange={(e) => this.context.actions.updateNewRepoField(dockerRepoNameKey, e)} />
			</div>
		);
	}
	renderWebhook(){
		let webhookData = this.context.state.addRepo.testNotification;
		let statusCode = NPECheck(webhookData, 'response/httpStatusCode', null);
		let status = NPECheck(this.context.state, 'addRepo/testNotificationStatus', null);
		let className = this.inputClassName(targetKey);

		if(status == 'SUCCESS') className += ' SuccessBg';
		if(status == 'ERROR') className += ' ErrorBg';

		return (
			<div className="">
				<div className="Row FlexColumn">
					<label>
						Webhook URL
					</label>
					<div className="FlexRow">
						
							<input className={className} 
							       value={this.context.state.addRepo.newRepo[targetKey]}
								   placeholder="Enter Webhook URL.."
							       onChange={(e) => this.context.actions.updateNewRepoField(targetKey, e)} />
						<div>
							{this.renderTestNotificationStatus(status, statusCode)}
						</div>
						{this.renderTestNotification()}
						
					</div>
				</div>
				<div className="Row FlexColumn">
					<label>
						Secret (optional)
					</label>
					<input className={this.inputClassName(secretKey)} 
					       value={this.context.state.addRepo.newRepo[secretKey]}
						   placeholder="Enter Secret"
					       onChange={(e) => this.context.actions.updateNewRepoField(secretKey, e)} />
				</div>
			</div>
		);
	}
	renderTestNotification(){
		return (
			<Btn style={{marginLeft: '7px'}}
				 onClick={() => this.context.actions.testNotification()}
				 text="Test Webhook"
				 canClick={true}/>
		);
	}
	renderTestNotificationStatus(status, statusCode){
		let icon = 'icon icon-dis-blank';
		let statusText = "See Test Results Here";
		let className = "InActive";

		if (status == 'SUCCESS') {
			icon = 'icon icon-dis-check'
			statusText = 'Success';
			className="Success";
		}

		if(status == 'WARNING') {
			icon = 'icon icon-dis-warning';
			statusText = "Warning";
			className="Warning";
		}

		if(status == 'ERROR') {
			icon = "icon icon-dis-alert";
			statusText = "Error";
			className = "Error";
		}

		className = "Status " + className;

		return (
			<div className="NotificationTestActions">
				<div className={className}>
					<span className="StatusText">{statusText}</span>&nbsp;
					<span className="StatusCode">{ (statusCode) ? `(${statusCode})` : null}</span>&nbsp;
					<span className="ViewTestResults" 
						  onClick={() => this.context.actions.toggleShowNotificationTestResults()}>
						{ (statusCode) ? ' - View Details' : null}
					</span>
				</div>
			</div>
		);
	}
	renderWebhookData(webhookData){
		if(this.context.state.addRepo.showNotificationTestResults) {
			return (
				<WebhookData webhookData={webhookData} 
							 modal={true}
							 close={ () => this.context.actions.toggleShowNotificationTestResults() }/>
			)
		};
	}
	renderErrorMsg(){
		if(this.context.state.addRepo.errorMsg) {
			return (
				<Msg
					text={this.context.state.addRepo.errorMsg}
				/>
			);
		}
	}
	renderSuccessMsg(){
		if(this.context.state.addRepo.success) {

			let message = 'Successfullt added repository to monitor';

			return (
				<Msg text={message} 
				     isSuccess={true}
				     close={ () => this.context.actions.clearAddRepoSuccess() }/>
			);
		}
	}
	renderLoader(){
		return (
			<Loader />
		);
	}
	renderAddRepoButton(){
		let canAdd = this.context.actions.canAddRepo();

		return (
			<div className="FlexRow JustifyCenter RowPadding">
				<Btn className="LargeBlueButton"
					 onClick={ () => this.addRepo() }
					 text="Connect Repository"
					 canClick={canAdd} />
			</div>
		);
	}
	addRepo(){
		if(this.context.state.addRepo.newRepoCredsType == 'NEW') {
			this.context.actions.addRegistryRequest()
			.then((credId) => this.context.actions.selectCredsForNewRepo(null, credId))
			.then(() => this.context.actions.addRepoRequest(this.toRepoList))
			.catch(() => console.error('Add Registry Errors -- Skipping add repo'))
		} else {
			this.context.actions.addRepoRequest(this.toRepoList.bind(this));
		}
	}
	toRepoList(){
		this.context.router.push('/repositories');
	}
	renderAddRepository(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-disk',
                renderBody: this.renderDockerRepository.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-webhook-circle-solid',
                renderBody: this.renderWebhook.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderErrorMsg.bind(this),
                condition: this.context.state.addRepo.errorMsg
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderLoader.bind(this),
                condition: this.context.state.addRepo.XHR
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderSuccessMsg.bind(this),
                condition: this.context.state.addRepo.success
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderAddRepoButton.bind(this)
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
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Connect a repository
					</h2>
				</div>
				<div>
					{this.renderWebhookData(this.context.state.addRepo.testNotification)}
				    {this.renderAddRegistry()}
					{this.renderAddRepository()}
				</div>
			</div>
		);
	}
}

AddRepository.childContextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

AddRepository.contextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
