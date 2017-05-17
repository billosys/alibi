module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
      pkg: grunt.file.readJSON('package.json'),
      copy: {
        clientsidejs: {
          files:[{
            expand:true,
            cwd: 'node_modules/js-joda/dist/',
            src: '**/*',
            dest: '../../resources/public/dist/external/js-joda/'
          },{
            expand:true,
            cwd: 'node_modules/selectize/dist/',
            src: '**/*',
            dest: '../../resources/public/dist/external/selectize/'
          },{
            expand:true,
            cwd: 'node_modules/fuzzy/lib/',
            src: '**/*',
            dest: '../../resources/public/dist/external/fuzzy/'
          }]
        }
      }
  });

  grunt.loadNpmTasks('grunt-contrib-copy');
  // Default task(s).
  grunt.registerTask('default', "log", ()=>grunt.log.write("Use the copy task"));

};
