import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'
import ContentRow from './../components/ContentRow'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import isEmpty from './../util/IsEmpty'

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
		return (
			<div className="">
				<div className="Row FlexColumn">
					<label>
						Webhook URL
					</label>
					<input className={this.inputClassName(targetKey)} 
					       value={this.context.state.addRepo.newRepo[targetKey]}
						   placeholder="Enter Webhook URL.."
					       onChange={(e) => this.context.actions.updateNewRepoField(targetKey, e)} />
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
		let webhookData = this.context.state.addRepo.testNotification;
		
		return (
			<div className="FlexColumn">
				<div className="FlexRow">
					<Btn onClick={() => this.context.actions.testNotification()}
						 text="Test Webhook"
						 canClick={true}/>
					 
					{this.renderTestNotificationStatus(webhookData)}
					{this.renderHideShowWebhookData()}
				</div>
				<div className="FlexRow">
					{this.renderWebhookData(webhookData)}
				</div>
			</div>
		);
	}
	renderHideShowWebhookData(){
		if(!isEmpty(this.context.state.addRepo.testNotification)) {

			let buttonText = (this.context.state.addRepo.showNotificationTestResults) ? 'Hide Webhook Content' 
																					  : 'Show Webhook Content';
			
			return (
				<div className="FlexRow Flex1 AlignCenter FlexEndJustify">
					<div className="ThickBlueText" onClick={() => this.context.actions.toggleShowNotificationTestResults()}>
						{buttonText}
					</div>
				</div>
			);
		}
	}
	renderTestNotificationStatus(webhookData){
		
		let statusCode = (webhookData.response) ? webhookData.response.httpStatusCode : null;

		let icon = 'icon icon-dis-blank';
		let statusText = "See Test Results Here";
		let className = "InActive";

		let isSuccess = null;
		let isWarning = null;
		let isError = null;

		if (statusCode) {
			isSuccess = 200 <= statusCode && statusCode <= 299;
			isWarning = (0 <= statusCode && statusCode <= 199) || (300 <= statusCode && statusCode <= 399);
			isError = 400 <= statusCode;	
		}


		if (isSuccess) {
			icon = 'icon icon-dis-check'
			statusText = 'Success';
			className="Success";
		}

		if(isWarning) {
			icon = 'icon icon-dis-warning';
			statusText = "Warning";
			className="Warning";
		}

		if(isError) {
			icon = "icon icon-dis-alert";
			statusText = "Error";
			className = "Error";
		}

		className = "Status " + className;

		return (
			<div className="NotificationTestActions">
				<div className={className}>
					<i className={icon}/>
					<span className="StatusText">{statusText}</span>
					<span className="Label">&nbsp;{(statusCode )? '- Response Code:' : null}&nbsp;</span>
					<span className="StatusCode">{statusCode}</span>
				</div>
			</div>
		);
	}
	renderWebhookData(webhookData){
		if(this.context.state.addRepo.showNotificationTestResults) {
			return (
				<WebhookData webhookData={webhookData}/>
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
				     close={() => this.context.actions.clearAddRepoSuccess()}/>
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
					 onClick={() => this.addRepo()}
					 text="Add Repository"
					 canClick={canAdd}
					 />
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
                icon:'icon icon-dis-container',
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
                renderBody: this.renderTestNotification.bind(this)
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
						Add Repository
					</h2>
				</div>
				<div>
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
