FROM ubuntu:xenial

# install tools
RUN apt-get update
RUN apt-get install -yq wget \
                        curl \
                        zip \
                        unzip \
                        software-properties-common \
                        apt-transport-https \
                        xvfb \
                        default-jdk \
                        openjfx \
                        locales \
			bc

# fix non-ascii characters.
RUN locale-gen en_US.UTF-8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -

# install nodejs 8
RUN curl -sL https://deb.nodesource.com/setup_8.x -o nodesource_setup.sh
RUN bash nodesource_setup.sh
RUN apt-get install -y nodejs

# install pip and PyYAML and requests
RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
RUN python get-pip.py
RUN pip install PyYAML requests

# add apt repos before installing docker and chrome
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu xenial stable"
RUN sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'

# install chrome docker and sshpass
RUN apt-get update
RUN apt-get install -y jq google-chrome-stable sshpass docker-ce
RUN apt-get install -y maven gradle

# configure npm and install angular cli
RUN npm config set registry https://build-artifactory.eng.vmware.com/artifactory/api/npm/npm
RUN npm install -g @angular/cli@latest

# start xvfb server
RUN Xvfb :20 -screen 0 1680x1050x16 -ac &
ENV DISPLAY=:20.0

CMD ["/bin/bash"]

