#!/usr/bin/env ruby

GRAALVM_LINUX_URL = 'https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.3.1/graalvm-ce-java11-linux-amd64-21.3.3.1.tar.gz'.freeze
GRAALVM_MACOS_URL = 'https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.3.1/graalvm-ce-java11-darwin-amd64-21.3.3.1.tar.gz'.freeze

OS_LINUX = 1
OS_MAC = 2

def os_type
  kernel = `uname | head -1`.strip
  case kernel
  when 'Linux'
    OS_LINUX
  when 'Darwin'
    OS_MAC
  else
    puts 'Unsupported OS'
    exit 1
  end
end

def graalvm_url
  case os_type
  when OS_LINUX
    GRAALVM_LINUX_URL
  when OS_MAC
    GRAALVM_MACOS_URL
  end
end

def graalvm_bin_path(graalvm_path)
  case os_type
  when OS_LINUX
    "#{graalvm_path}/bin"
  when OS_MAC
    "#{graalvm_path}/Contents/Home/bin"
  end
end

def exit_if_not_project_root
  return if `pwd | awk -F'/' '{print $NF}'`.strip == 'kp-diff'

  puts 'Script should be launched from project root'
  exit 1
end

def project_root_dir
  `pwd`.strip
end

def app_version
  `grep "^version=" #{project_root_dir}/src/main/resources/version.properties | cut -d= -f2`.strip
end

def compiled_binary_path
  "#{project_root_dir}/kp-diff-#{app_version}"
end

def find_graalvm_dir_path
  ls = `ls -la #{project_root_dir}/tmp | grep '^d.*graalvm'`
  if ls.empty? == false
    dir_name = ls.split(' ').last.strip
    "#{project_root_dir}/tmp/#{dir_name}"
  else
    nil
  end
end

def graal_vm_dir_path
  tmp_dir_path = "#{project_root_dir}/tmp"
  graalvm_archive_path = "#{tmp_dir_path}/graalvm.tar.gz"
  graalvm_dir_path = find_graalvm_dir_path

  return graalvm_dir_path if graalvm_dir_path.nil? == false

  File.exist?(tmp_dir_path) == false && `mkdir #{tmp_dir_path}`

  unless File.exist?(graalvm_archive_path)
    puts 'Downloading archive'
    `wget -q --show-progress -O #{graalvm_archive_path} #{graalvm_url}`
  end

  if File.exist?(graalvm_archive_path)
    puts 'Extractring archive'
    `tar -zxf #{graalvm_archive_path} --directory #{tmp_dir_path}`
  else
    puts "Unable to locate file: #{graalvm_archive_path}"
    exit 1
  end

  find_graalvm_dir_path
end

def setup_graal_vm(graalvm_path)
  native_image_path = "#{graalvm_bin_path(graalvm_path)}/native-image"
  gu_path = "#{graalvm_bin_path(graalvm_path)}/gu"

  unless File.exist?(native_image_path)
    puts 'Installing native-image'
    `#{gu_path} install native-image`
  end
end

def remove_compiled_file
  if File.exist?(compiled_binary_path)
    puts "Remove previously compiled binary: #{compiled_binary_path}"
    `rm #{compiled_binary_path}`
  end
end

def compile_binary(graalvm_path)
  native_image_path = "#{graalvm_bin_path(graalvm_path)}/native-image"

  if File.exist?(native_image_path)
    puts 'Compiling'
    `export JAVA_HOME=#{graalvm_path}`
    `cp #{project_root_dir}/build/libs/kp-diff-#{app_version}-all.jar #{project_root_dir}/build/libs/kp-diff-#{app_version}.jar`
    `#{native_image_path} --no-server --no-fallback -H:IncludeResources=".*\.properties" --allow-incomplete-classpath -jar #{project_root_dir}/build/libs/kp-diff-#{app_version}.jar`
  else
    puts "Unable to locate file: #{native_image_path}"
    exit 1
  end
end

def print_result
  if File.exist?(compiled_binary_path)
    puts "Successfully compiled: #{compiled_binary_path}"
  else
    puts 'Failed to compile binary'
  end
end

def main
  exit_if_not_project_root
  graalvm_path = graal_vm_dir_path
  setup_graal_vm(graalvm_path)
  remove_compiled_file
  compile_binary(graalvm_path)
  print_result
end

main
