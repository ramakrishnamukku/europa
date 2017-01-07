/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Dropzone from 'react-dropzone'

export default class UploadGCEServciceAccount extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}	
	handleDrop(files){
		let file = files[0]
		let reader = new FileReader();

		reader.onload = (e) => {
			let serviceJSON = e.currentTarget.result;
			this.props.handleDrop(serviceJSON);
		};

		reader.readAsText(file);
	}
	renderMessage(){
		if(this.props.isComplete) {
			return (
				<span className="Error UploadText" onClick={() => this.props.cancel()}>
					<i className="i icon-dis-close Error" />
					Remove service account credential
				</span>
			);
		}

		let message = (this.props.isEdit) ? 'Update service account credential' : 'Upload service account credential'

		return (
			<span className="UploadText">{message}</span>
		);
	}
	render() {
		let className = (this.props.standaloneMode) ? 'UploadGCEServiceAccount StandAlone' : 'UploadGCEServiceAccount'

		return (
			<div className={className}>
				<Dropzone className="UploadDropZone" 
						  multiple={false} 
						  onDrop={(files) => this.handleDrop(files)}
						  disableClick={this.props.isComplete}>
					{this.renderMessage()}
				</Dropzone>

			</div>
		);
	}
}

UploadGCEServciceAccount.propTypes = {
	handleDrop: PropTypes.func.isRequired,
	cancel: PropTypes.func.isRequired,
	isComplete: PropTypes.bool.isRequired,
	standaloneMode: PropTypes.bool,
	isEdit: PropTypes.bool,
}

UploadGCEServciceAccount.childContextTypes = {
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

UploadGCEServciceAccount.contextTypes = {
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
