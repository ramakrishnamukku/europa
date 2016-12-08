import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'
import Loader from './../components/Loader'
import Btn from './../components/Btn'
import Msg from './../components/Msg'

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
		let provider = 'provider';
		return (
			<div className="FlexColumn">
				<label>
					Docker Registry Provider
				</label>
				<select className={this.inputClassName(provider)}
						value={this.context.state.addRegistry.newRegistry[provider]}
				        onChange={(e) => this.context.actions.updateNewRegistryField(provider, e)}>
				   <option value="">Select Amazon Container Registry or Google Container Registry</option>
				   <option value="GCR">Google Container Registry</option>
				   <option value="ECR">Amazon Container Registry</option>
				   <option value="DOCKERHUB">DockerHub</option>
				   <option value="PRIVATE">Private Registry</option>
				</select>
			</div>
		);
	}
	renderRegistryCredentials() {
		let keyName = 'name';
		let region = 'region';
		let key = 'key';
		let secret = 'secret';

		let readOnly = {};

		if(this.props.isEdit) {
			readOnly['readOnly'] = 'readOnly';
			readOnly['disabled'] = 'disabled';
		}

		return (
			<div className="FlexColumn">
				<label>
					Registry Credentials
				</label>
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
                icon:'icon icon-dis-docker',
                renderBody: this.renderSelectProvider.bind(this)
            }]
		}, {
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
		}, {
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