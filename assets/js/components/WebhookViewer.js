/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import RadioButton from './../components/RadioButton'

export default class WebhookViewer extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render() {	
		return (
			<div className="WebhookViewer">
				
			</div>
		);		
	}
}

WebhookViewer.propTypes = {
	allWebhookData: PropTypes.object
};

WebhookViewer.childContextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

WebhookViewer.contextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
