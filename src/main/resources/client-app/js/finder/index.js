window.Showdown = require('showdown')
window.Request = require('superagent');
window.Button = ReactBootstrap.Button;
window.Modal = ReactBootstrap.Modal;
window.OverlayMixin = ReactBootstrap.OverlayMixin;
window.ProgressBar = ReactBootstrap.ProgressBar;
window.Header = require('../components/header');
require('bootstrap-contextmenu');


FinderActions = Reflux.createActions([
    "load",
    "loaded",
    "refresh",
    "download",
    "playVideo",
    "playAudio",
    "select",
    "unSelect",
    "openFile",
    "updateUploadProgress",
    "upload"
]);

FinderListStore = Reflux.createStore({
    init: function () {
        this.listenTo(FinderActions.load, this.fetchData);
        this.listenTo(FinderActions.refresh, this.refresh);
    },
    fetchData: function (dir) {
        dir = dir || '';
        this.dir = dir;
        Request.get('/rest/finder/list.html/?dir=' + dir, function (res) {
            this.list = JSON.parse(res.text);
            FinderActions.loaded(dir);
            this.trigger(this.list);
        }.bind(this));
    },
    refresh: function () {
        this.fetchData(this.dir);
    },
    getInitialState: function () {
        this.list = [];
        this.dir = '';
        return this.list;
    }
});

FinderUploadStore = Reflux.createStore({
    getInitialState: function () {
        this.progresses = [];
        return this.progresses;
    },
    init: function () {
        this.listenTo(FinderActions.updateUploadProgress, this.updateUploadProgress);
        this.listenTo(FinderActions.upload, this.upload);
    },
    updateUploadProgress: function (progress) {
        if (progress) {
            var i = 0;
            for (i = 0; i < this.progresses.length; i++) {
                if (this.progresses[i].file.name == progress.file.name) {
                    this.progresses[i] = progress;
                    break;
                }
            }
            if (i == this.progresses.length) {
                this.progresses.push(progress);
            }

        }
        this.trigger(this.progresses);
    },
    upload: function (currentDir) {
        for (var progress  in this.progresses) {
            var xhr = new XMLHttpRequest();
            var formData = new FormData;
            formData.append('file', progress.file);
            formData.append('dir', currentDir);
            xhr.open('post', '/rest/finder/upload.html/', true);
            xhr.upload.addEventListener('progress', function (e) {
                progress.progress = (Math.ceil(e.loaded / e.total) * 100);
                this.trigger(this.progresses);
            }.bind(this), false);
            xhr.onreadystatechange = function (e) {
                if (4 == this.readyState) {
                    FinderActions.load(currentDir);
                } else {

                }
            };
            console.log(formData);
            xhr.send(formData);
        }
    }
});

var FileContextMenu = React.createClass({
    render: function () {
        return (
            <div id="context-menu">
                <ul className="dropdown-menu" role="menu">
                    <li action="delete">
                        <a>Delete</a>
                    </li>
                    <li action="rename">
                        <a>Rename</a>
                    </li>
                </ul>
            </div>
        );
    }
});

var DirContextMenu = React.createClass({
    render: function () {
        return (
            <div id="dir-context-menu">
                <ul className="dropdown-menu" role="menu">
                    <li action="mkdir">
                        <a >Create Dir</a>
                    </li>
                </ul>
            </div>
        );
    }
})

var File = React.createClass({
    mixins: [Reflux.listenTo(FinderActions.select, "onSelectAction"), Reflux.listenTo(FinderActions.unSelect, "onUnSelectAction"), Reflux.listenTo(FinderActions.openFile, "onOpenAction")],
    onSelectAction: function (selectedFile) {
        if (selectedFile.name == this.props.file.name) {
            this.setState({selected: true});
        } else {
            this.setState({selected: false});
        }
    },
    onUnSelectAction: function () {
        this.setState({selected: false});
    },
    onOpenAction: function (file) {
        if (file.name == this.props.file.name) {
            open();
        }
    },
    getInitialState: function () {
        return {iconUrl: this.getIconUrl(this.props.file), selected: false};
    },
    componentWillReceiveProps: function (nextProps) {
        this.setState({iconUrl: this.getIconUrl(nextProps.file)});
    },

    getIconUrl: function (file) {
        if (file.isDir) {
            return '/img/file-type-icon/folder-icon.png';
        }
        var name = file.name;
        var ext = name.substring(name.lastIndexOf('.') + 1);
        return '/img/file-type-icon/' + ext + '-icon.png';
    },
    open: function () {
        var file = this.props.file;
        if (this.props.file.isDir) {
            FinderActions.load(file.path);
        } else {
            var name = file.name;
            var ext = name.substring(name.lastIndexOf('.') + 1);
            if (ext == 'mp4' || ext == 'mov') {
                FinderActions.playVideo(file.path);
            } else if (ext == 'mp3') {
                FinderActions.playAudio(file.path);
            } else {
                window.location.href = '/rest/finder/download.html/?path=' + this.props.file.path;
            }
        }
    },
    componentDidMount: function () {
        var self = this;
        $(this.refs.fileDiv.getDOMNode()).contextmenu({
            target: '#context-menu',
            before: function (context, e) {
                FinderActions.select(self.props.file);
                return true;
            },
            onItem: function (context, e) {
                var action = $(e.currentTarget).attr("action");
                console.log(action);
                if (action == 'delete') {
                    if (confirm('Are you sure you want delete this file/dir?')) {
                        Request.post('/rest/finder/' + action + '.html/').type('form').send({path: self.props.file.path}).end(function (res) {
                            FinderActions.refresh();
                        });
                    }
                } else if (action == 'rename') {
                    var newName = prompt('Please enter new name: ');
                    Request.post('/rest/finder/' + action + '.html/').type('form').send({
                        path: self.props.file.path,
                        newName: newName
                    }).end(function (res) {
                        FinderActions.refresh();
                    });
                }

            }
        });
    },
    onClickEvent: function () {
        if (this.props.file.isDir) {
            FinderActions.unSelect();
            this.open();
        } else {
            FinderActions.select(this.props.file);
        }
    },
    render: function () {
        var style = {
            flaot: 'left',
            display: 'inline-block',
            backgroundColor: this.state.selected ? '#dddddd' : '#ffffff',
            width: '150'
        };

        return (
            <div onContextMenu={this.onClickEvent} onClick={this.onClickEvent} onDoubleClick={this.open} style={style} ref="fileDiv">
                <img src={this.state.iconUrl}/>
                <div>{this.props.file.name}</div>
            </div>
        );
    }
})


var Finder = React.createClass({
    mixins: [Reflux.connect(FinderListStore, "list"), Reflux.connect(FinderActions.loaded, "currentDir")],
    componentDidMount: function () {
        var self = this;
        $(this.refs.finderDiv.getDOMNode()).contextmenu({
            target: '#dir-context-menu',
            onItem: function (context, e) {
                var action = $(e.currentTarget).attr("action");
                var dirName = prompt('Please enter directory name: ');
                Request.post('/rest/finder/' + action + '.html/').type('form').send({
                    path: self.state.currentDir,
                    dirName: dirName
                }).end(function (res) {
                    FinderActions.refresh();
                });
            }

        });
    },
    render: function () {
        return (
            <div ref="finderDiv">
            {this.state.list.map(function (elem) {
                return (
                    <File file={elem}>
                    </File>
                );
            }, this)}
            </div>
        )
    }
});

var VideoPlayer = React.createClass({
    componentDidMount: function () {
        jwplayer('videoPlayer').setup({
            file: 'http://example.com/media/my_video.mp4',
            image: '//www.longtailvideo.com/content/images/jw-player/lWMJeVvV-876.jpg',
            width: '100%',
            aspectratio: '16:9'
        });
    },
    render: function () {
        return <div id='videoPlayer'></div>;
    }
});

var PlayerModal = React.createClass({
    mixins: [OverlayMixin, Reflux.listenTo(FinderActions.playAudio, "onPlayAudio"), Reflux.listenTo(FinderActions.playVideo, "onPlayVideo")],
    onPlayVideo: function (videoFilePath) {
        this.setState({
            isModalOpen: true,
            videoFilePath: videoFilePath
        });
    },
    onPlayAudio: function (audioFilePath) {
        this.setState({
            isModalOpen: true,
            audioFilePath: audioFilePath
        });
    },
    getInitialState: function () {
        return {
            isModalOpen: false
        };
    },

    handleToggle: function () {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
    },

    render: function () {
        return (
            <span/>
        );
    },

    renderOverlay: function () {
        if (!this.state.isModalOpen) {
            return <span/>;
        }

        return (
            <Modal title="Player" onRequestHide={this.handleToggle}>
                <div className="modal-body">
                    <VideoPlayer />
                </div>
            </Modal>
        );
    }
});

var FileUploadModal = React.createClass({
    mixins: [OverlayMixin, Reflux.connect(FinderActions.loaded, "currentDir")],
    getInitialState: function () {
        return {
            isModalOpen: false,
            currentDir: ''
        };
    },

    handleToggle: function () {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
    },

    render: function () {
        return (
            <Button onClick={this.handleToggle} bsStyle="primary">Upload</Button>
        );
    },

    renderOverlay: function () {
        if (!this.state.isModalOpen) {
            return <span/>;
        }

        return (
            <Modal title="File Upload" onRequestHide={this.handleToggle}>
                <div className="modal-body">
                    <FinderUpload currentDir={this.state.currentDir}/>
                </div>
            </Modal>
        );
    }
});


var FinderUpload = React.createClass({
    mixins: [Reflux.connect(FinderUploadStore, "progresses")],
    componentDidMount: function () {
        FinderActions.updateUploadProgress();
    },
    setFile: function () {
        this.file = this.refs.file.getDOMNode().files[0];
        var progress = {}
        progress.file = this.file;
        progress.progress = 0;
        FinderActions.updateUploadProgress(progress);
    },
    upload: function (e) {
        e.preventDefault();
        FinderActions.upload(this.props.currentDir);
    },
    render: function () {
        return (
            <form>
                Current Directory: {this.props.currentDir}
                <input type="file" ref="file" onChange={this.setFile}/>
                {this.state.progresses.map(function (elem) {
                    return (
                        <div>
                            <span>{elem.file.name}</span>
                            <ProgressBar active now={elem.progress} />
                        </div>
                    );
                }, this)}
                <input type="submit"
                    value="Upload" onClick={this.upload}/>
            </form>
        )
    }
});

var Footer = React.createClass({
    mixins: [Reflux.listenTo(FinderActions.select, "onSelectAction"), Reflux.listenTo(FinderActions.unSelect, "onUnSelectAction")],
    onSelectAction: function (selectedFile) {
        this.setState({selectedFile: selectedFile, selected: true});
    },
    onUnSelectAction: function () {
        this.setState({selected: false});
    },
    getInitialState: function () {
        return {};
    },
    open: function (e) {
        e.preventDefault();
        FinderActions.openFile(this.state.selectedFile);
    },
    render: function () {
        var footerStyle = {
            textAlign: 'center',
            position: 'fixed',
            bottom: '0',
            width: '100%',
            display: this.state.selected ? 'block' : 'none'
        };
        var btnStyle = {
            width: '500'
        };
        return (
            <Navbar style={footerStyle}>
                <Button bsStyle="primary" bsSize="large" style={btnStyle} onClick={this.open}>Open</Button>
            </Navbar>
        )
    }
});


React.render((<div>
    <Header/>
    <DirContextMenu/>
    <FileContextMenu/>
    <PlayerModal/>
    <Finder />
    <FileUploadModal/>
    <Footer/>
</div>), document.getElementById('content'));

FinderActions.load();