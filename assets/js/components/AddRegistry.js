import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'
import Loader from './../components/Loader'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import RadioButton from './../components/RadioButton'
import RegistryNames from './../util/RegistryNames'

let provider = 'provider';
let keyName = 'name';
let region = 'region';
let key = 'key';
let secret = 'secret';

export default class AddRegistry extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillUnmount() {
		this.context.actions.resetAddRegistryState();	
	}
	inputClassName(selector){
		let hasSelector = this.context.state.addRegistry.errorFields.includes(selector)
		if(hasSelector) {
			return "BlueBorder FullWidth Error";
		} else {
		    return "BlueBorder FullWidth";
		}
	}
	renderSelectProvider(){
		let readOnly = {};

		if(this.props.isEdit) {
			readOnly['readOnly'] = 'readOnly';
			readOnly['disabled'] = 'disabled';
		}
		return (
			<div className="Flex1">
				<label className="small">
					Docker Registry Provider {(this.props.isEdit) ? '( Read Only )' : null}
				</label>
				<select className={this.inputClassName(provider)}
						value={this.context.state.addRegistry.newRegistry[provider]}
				        onChange={(e) => this.context.actions.updateNewRegistryField(provider, e)}
		       			{...readOnly}>
				   <option value="">Select Amazon Container Registry or Google Container Registry</option>
				   {Object.keys(RegistryNames).map((key) => {
				   		return (
				   			<option key={key} value={key}>{RegistryNames[key]}</option>
				   		);
				   })}
				</select>
			</div>
		);
	}
	renderRegistryCredentials() {
		return (
			<div className="FlexColumn">
				<label>
					Registry Credentials
				</label>
				{this.renderChooseCredsTypes()}
				{this.renderExistingRegistryCredentials()}
				{this.renderNewRegistryCredentials()}
			</div>
		);
	}
	renderExistingRegistryCredentials() {
		if(this.context.state.addRepo.newRepoCredsType == 'EXISTING' && !this.props.standaloneMode) {
			return (
				<div className="Flex1">
					<label className="small">
						Select Credentials
					</label>
					<div className="Flex1">
						<select className="BlueBorder FullWidth"
						        onChange={(e) => this.context.actions.selectCredsForNewRepo(e)}>
						    <option value="">Select Credentials</option>
							{this.context.state.registries.map((reg, index) => {
								return (
									<option value={JSON.stringify(reg)} key={index}>{reg.provider} - {reg.name} - {reg.region}</option>
								);
							})}
						</select>
					</div>
				</div>
			);
		}
	}
	renderNewRegistryCredentials(){
		if(this.context.state.addRepo.newRepoCredsType == 'NEW' || this.props.standaloneMode) {

			let readOnly = {};

			if(this.props.isEdit) {
				readOnly['readOnly'] = 'readOnly';
				readOnly['disabled'] = 'disabled';
			}

			return (
				<div className="FlexColumn">
				 	<div className="FlexRow Row"> 
						{this.renderSelectProvider()}
					</div>
					<div className="FlexRow Row">
						<div className="Flex1 Column">
							<label className="small">
								Key Name {(this.props.isEdit) ? '( Read Only )' : null}
							</label>
							<input className={this.inputClassName(keyName)}
								   value={this.context.state.addRegistry.newRegistry[keyName]}
							       placeholder="Enter Key Name.."
								   onChange={(e) => this.context.actions.updateNewRegistryField(keyName, e)} 
								   {...readOnly}/>
						</div>
						<div className="Flex1">
							<label className="small">
								Key Region {(this.props.isEdit) ? '( Read Only )' : null}
							</label>
							<select className={this.inputClassName(region)}
									value={this.context.state.addRegistry.newRegistry[region]}
							        onChange={(e) => this.context.actions.updateNewRegistryField(region, e)}
									{...readOnly}>
							   <option value="">Select Region...</option>
							   <option value="us-west-1">us-west-1</option>
							   <option value="us-west-2">us-west-2</option>
							   <option value="us-east-1">us-east-1</option>
							   <option value="us-east-2">us-east-2</option>
							</select>
						</div>
					</div>
					<div className="FlexRow Row">
						<div className="Flex1 Column">
							<label className="small">
								Public Key
							</label>
							<input className={this.inputClassName(key)}
								   value={this.context.state.addRegistry.newRegistry[key]}
								   placeholder="Enter Public Key.."
								   onChange={(e) => this.context.actions.updateNewRegistryField(key, e)} />
						</div>
						<div className="Flex1">
							<label className="small">
								Private Key
							</label>
							<input className={this.inputClassName(secret)}
								   value={this.context.state.addRegistry.newRegistry[secret]}
								   placeholder="Enter Private Key.."
								   onChange={(e) => this.context.actions.updateNewRegistryField(secret, e)} />
						</div>
					</div>
				</div>
			);
		}
	}
	renderChooseCredsTypes(){
		if(!this.props.standaloneMode) {
			return (
				<div className="FlexRow RowPadding">
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('EXISTING')} 
									 isChecked={this.context.state.addRepo.newRepoCredsType == 'EXISTING'}
									 label="Existing Credentials" />
					</div>
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('NEW')} 
							  		 isChecked={this.context.state.addRepo.newRepoCredsType == 'NEW'}
									 label="New Credentials" />
					</div>
				</div>
			);
		}
	}
	renderErrorMsg(){
		if(this.context.state.addRegistry.errorMsg) {
			return (
				<Msg
					text={this.context.state.addRegistry.errorMsg}
				/>
			);
		}
	}
	renderSuccessMsg(){
		if(this.context.state.addRegistry.success) {

			let message = `Successfully ${(this.props.isEdit) ? 'updated' : 'added'} registry credentials`;

			return (
				<Msg text={message} 
				     isSuccess={true}
				     close={() => this.context.actions.clearAddRegistrySuccess()}/>
			);
		}
	}
	renderLoader(){
		return (
			<Loader />
		);
	}
	renderAddButton(){
		return (
			<Btn onClick={() => this.context.actions.addRegistryRequest()}
				 text={(this.props.isEdit) ? 'Save Registry' : 'Add Registry'}
				 canClick={this.context.actions.canAddRegistry()}
				 help="Clicking this button will save the specifed credentials to the monitor."/>
		);
	}
	renderAddRegistry(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-credential',
                renderBody: this.renderRegistryCredentials.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderErrorMsg.bind(this),
                condition: this.context.state.addRegistry.errorMsg
            }]
		}];

		if(this.props.standaloneMode) {
			let standAloneRows = [{
				columns: [{
	                icon:'icon icon-dis-blank',
	                renderBody: this.renderLoader.bind(this),
	                condition: this.context.state.addRegistry.XHR
	            }]
			}, {
				columns: [{
	                icon:'icon icon-dis-blank',
	                renderBody: this.renderSuccessMsg.bind(this),
	                condition: this.context.state.addRegistry.success
	            }]
			}, {
				columns: [{
	                icon:'icon icon-dis-blank',
	                renderBody: this.renderAddButton.bind(this),
	                condition: this.props.standaloneMode
	            }]
			}]

			rows = rows.concat(standAloneRows);
		}

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
				<div>
					{this.renderAddRegistry()}
				</div>
			</div>
		);
	}
}

AddRegistry.propTypes = {
	standaloneMode: React.PropTypes.bool,
	isEdit: React.PropTypes.bool
};

AddRegistry.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

AddRegistry.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};