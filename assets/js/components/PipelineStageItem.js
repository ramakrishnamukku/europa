import React, { Component, PropTypes } from 'react'
import { Link } from 'react-router'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import CenteredConfirm from './../components/CenteredConfirm'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'

export default class PipelineStageItem extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deleteToggled: false,
      pipelineComponent: this.props.pipelineComponent
    };
  }
  renderTrigger() {
    if (this.props.empty) return;
    // Don't render for the last stage
    if (this.props.pipelineStore.pipeline.components.length - 1 == this.props.idx) return;
    if (this.props.pipelineStore.pipeline.components.length == 0
        && this.props.firstStage) return;

    return (
      <div className="stage-trigger">
        <div className="stage-trigger-pipe">
          <img src="/public/images/dis-pipeline-green.svg" />
        </div>
      </div>
    );
  }
  renderEmptyOption() {
    if (!NPECheck(this.props.stage, 'autoDeployTrigger', null)) {
      return <option value="">Select...</option>
    }
  }
  renderEmptyStage() {
    return (
      <div className="pipeline-stage-item">
        <div className="pipeline-grey-wrap">
          <div className="stage-destination-wrap">
            <div className="left-icon-col"
                 style={ {background: "#808285"} }>
              <i className="icon-dis-repo" />
            </div>
            <div className="stage-destination">
              <div className="stage-dest-interior">
                <div className="stage-dest-details">
                  <span className="stage-not-connected">You have not connected a repository</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
  renderStage(repo) {
    return (
      <div className="pipeline-stage-item">
        <div className="pipeline-grey-wrap">
          <div className="stage-destination-wrap">
            <div className="left-icon-col" style={ {background: "#2E5597"} }>
              <img src="/public/images/distelli-europa-mark.svg"/>
            </div>
            <div className="stage-destinations">
              {this.renderInterior(repo)}
            </div>
          </div>
        </div>
        {this.renderTrigger()}
      </div>
    );
  }
  renderDeleteStage() {
    if (!this.props.firstStage) {
      return (
        <div className="delete-stage">
          <i className="icon-dis-close"
             onClick={() => this.setState({deleteToggled: !this.state.deleteToggled})} />
        </div>
      );
    }
  }
  renderInterior(repo) {
    if (!this.props.firstStage
        && this.props.pipelineStore.removePipelineComponentXHR
        && this.props.pipelineStore.removePipelineComponentXHR == this.state.pipelineComponent.id) {
      return (
        <div className="stage-destination">
          <div style={ {margin: "15px 0 0"} }>
            <Loader />
          </div>
        </div>
      );
    }

    if (this.state.deleteToggled) {
      return (
        <div className="stage-destination">
          <CenteredConfirm onConfirm={ () => this.context.actions.removePipelineComponent(this.state.pipelineComponent.id) }
                           onCancel={ () => this.setState({deleteToggled: !this.state.deleteToggled}) }
                           confirmButtonStyle={{background: "#df423a"}}
                           confirmButtonText="Remove"
                           messageStyle={ {fontSize: ".75rem", margin: "7px 0 4px"} }
                           message="Are you sure you want to remove this stage?" />
        </div>
      );
    }

    let lastEvent = NPECheck(repo, 'lastEvent', {
      imageTags: [],
      imageSha: "N/A"
    });
    let friendlyTime = (lastEvent.eventTime) ? ConvertTimeFriendly(lastEvent.eventTime) : 'Unknown';

    return (
      <div className="stage-destination">
        <div className="stage-dest-interior">
          <div className="stage-dest-details">
            <div style={ {position: "relative", top: "2px"} }>
              <span style={{color: "#1DAFE9", fontSize: ".75rem", fontWeight: "900"}}>
                <Link to={`/repository/${repo.id}`}>
                  {repo.name}
                </Link>
              </span>
            </div>
            <div>
              <div className="meta-details">
                <strong>Last Pushed:</strong>
                <span>{friendlyTime}</span>
              </div>
              <div className="meta-details">
                <strong>Image SHA:</strong>
                <span>
                  { lastEvent.imageSha != "N/A"
                    ? `${lastEvent.imageSha.substring(7, lastEvent.imageSha.length)}`
                    : lastEvent.imageSha }
                </span>
              </div>
              <div className="meta-details">
                <strong>Tags:</strong>
                <span>
                  {lastEvent.imageTags.map((tag, index) => {
                    return (
                      <span className="Tag" key={index}>
                        {tag}
                      </span>
                    );
                  })}
                </span>
              </div>
            </div>
          </div>
          {this.renderDeleteStage()}
        </div>
      </div>
    );
  }
  render() {
    if (this.props.empty) {
      return this.renderEmptyStage();
    }

    return this.renderStage(this.props.repo);
  }
}

PipelineStageItem.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

PipelineStageItem.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

PipelineStageItem.propTypes = {
  empty: PropTypes.bool,
};
